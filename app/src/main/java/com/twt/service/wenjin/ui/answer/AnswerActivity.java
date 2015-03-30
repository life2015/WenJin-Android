package com.twt.service.wenjin.ui.answer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.twt.service.wenjin.R;
import com.twt.service.wenjin.api.ApiClient;
import com.twt.service.wenjin.bean.Answer;
import com.twt.service.wenjin.support.DateHelper;
import com.twt.service.wenjin.support.LogHelper;
import com.twt.service.wenjin.ui.BaseActivity;
import com.twt.service.wenjin.ui.common.PicassoImageGetter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AnswerActivity extends BaseActivity implements AnswerView {

    private static final String LOG_TAG = AnswerActivity.class.getSimpleName();

    private static final String PARAM_ANSWER_ID = "answer_id";
    private static final String PARAM_QUESTION = "question";

    @Inject
    AnswerPresenter mPresenter;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.pb_answer_loading)
    ProgressBar mPbLoading;
    @InjectView(R.id.iv_answer_avatar)
    ImageView ivAvatar;
    @InjectView(R.id.tv_answer_username)
    TextView tvUsername;
    @InjectView(R.id.tv_answer_signature)
    TextView tvSignature;
    @InjectView(R.id.tv_answer_content)
    TextView tvContent;
    @InjectView(R.id.tv_answer_add_time)
    TextView tvAddTime;

    public static void actionStart(Context context, int answerId, String question) {
        Intent intent = new Intent(context, AnswerActivity.class);
        intent.putExtra(PARAM_ANSWER_ID, answerId);
        intent.putExtra(PARAM_QUESTION, question);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        ButterKnife.inject(this);

        String question = getIntent().getStringExtra(PARAM_QUESTION);
        toolbar.setTitle(question);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int answerId = getIntent().getIntExtra(PARAM_ANSWER_ID, 0);
        LogHelper.v(LOG_TAG, "answer id: " + answerId);
        mPresenter.loadAnswer(answerId);
    }

    @Override
    protected List<Object> getModlues() {
        return Arrays.<Object>asList(new AnswerModule(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_answer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showProgressBar() {
        mPbLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mPbLoading.setVisibility(View.GONE);
    }

    @Override
    public void bindAnswerData(Answer answer) {
        if (answer.avatar_file != null) {
            Picasso.with(this).load(ApiClient.getAvatarUrl(answer.avatar_file)).into(ivAvatar);
        }
        tvUsername.setText(answer.user_name);
        tvSignature.setText(answer.signature);
        tvContent.setText(Html.fromHtml(answer.answer_content, new PicassoImageGetter(this, tvContent), null));
        tvAddTime.setText(DateHelper.formatAddDate(answer.add_time));
    }

    @Override
    public void toastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
