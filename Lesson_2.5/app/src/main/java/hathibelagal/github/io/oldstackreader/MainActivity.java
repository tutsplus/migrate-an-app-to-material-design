package hathibelagal.github.io.oldstackreader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView lvQuestions;
    private JSONReader reader;
    private Handler handler;
    private List<Question> questions;
    private RecyclerView.Adapter<QuestionViewHolder> adapter;

    // Default site
    private String site = "Stack Overflow";

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_nav);

        lvQuestions = (RecyclerView) findViewById(R.id.lv_questions);
        reader = new JSONReader();
        handler = new Handler();
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        drawer = (DrawerLayout)findViewById(R.id.drawer);
        nav = (NavigationView)findViewById(R.id.nav);

        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                site = item.getTitle().toString();
                updateScreen();
                drawer.closeDrawers();
                item.setChecked(true);
                return true;
            }
        });

        updateScreen();
    }

    /**
     * Reloads list of questions, and updates the title
     */
    private void updateScreen() {
        updateQuestions(StackExchangeSites.MAP.get(site));
        setTitle(site);
    }

    /**
     * Spawns a new thread and fetches a new list of questions
     * from the StackExchange API.
     * @param site
     */
    private void updateQuestions(final String site) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                questions = reader.fetchQuestionsFrom(site);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        renderList();
                    }
                });
            }
        }).start();
    }

    /**
     * Creates an adapter that can use the list of questions.
     * If adapter exists already, calls only notifyDatasetChanged
     */
    private void renderList() {
        if(adapter == null) {

            adapter = new RecyclerView.Adapter<QuestionViewHolder>() {
                @Override
                public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View convertView = getLayoutInflater().inflate(R.layout.question, null);
                    QuestionViewHolder viewHolder = new QuestionViewHolder(convertView);
                    viewHolder.score = (TextView)convertView.findViewById(R.id.question_score);
                    viewHolder.title = (TextView)convertView.findViewById(R.id.question_title);
                    viewHolder.author = (TextView)convertView.findViewById(R.id.question_author);
                    viewHolder.question = convertView.findViewById(R.id.question);
                    return viewHolder;
                }

                @Override
                public void onBindViewHolder(QuestionViewHolder viewHolder, final int position) {
                    viewHolder.score.setText("" + questions.get(position).getScore());
                    viewHolder.title.setText(questions.get(position).getTitle());
                    viewHolder.author.setText("Asked by " + questions.get(position).getAuthorDisplayName());

                    viewHolder.question.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(questions.get(position).getLink()));
                            startActivity(intent);
                        }
                    });
                }

                @Override
                public int getItemCount() {
                    return questions.size();
                }
            };

            lvQuestions.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            lvQuestions.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            drawer.openDrawer(GravityCompat.START);
        }

        if(item.getItemId() == R.id.menu_refresh) {
            updateScreen();
        }
        return super.onOptionsItemSelected(item);
    }
}
