package info.nightscout.androidaps.plugins.general.automation.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import info.nightscout.androidaps.R;
import info.nightscout.androidaps.plugins.general.automation.AutomationEvent;
import info.nightscout.androidaps.plugins.general.automation.AutomationFragment;
import info.nightscout.androidaps.plugins.general.automation.AutomationPlugin;
import info.nightscout.androidaps.plugins.general.automation.triggers.TriggerConnector;

public class EditEventDialog extends DialogFragment {
    private static AutomationEvent mEvent;

    @BindView(R.id.inputEventTitle)
    TextInputEditText mEditEventTitle;

    @BindView(R.id.editTrigger)
    TextView mEditTrigger;

    @BindView(R.id.editAction)
    TextView mEditAction;

    @BindView(R.id.triggerDescription)
    TextView mTriggerDescription;

    @BindView(R.id.actionListView)
    RecyclerView mActionListView;

    private Unbinder mUnbinder;
    private AutomationFragment.ActionListAdapter mActionListAdapter;

    public static EditEventDialog newInstance(AutomationEvent event) {
        mEvent = event; // FIXME

        Bundle args = new Bundle();
        EditEventDialog fragment = new EditEventDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.automation_dialog_event, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        // load data from bundle
        if (savedInstanceState != null) {
            String eventData = savedInstanceState.getString("event");
            if (eventData != null) mEvent.fromJSON(eventData);
        } else {
            mEvent.setTrigger(new TriggerConnector(TriggerConnector.Type.OR));
        }

        // display root trigger
        mTriggerDescription.setText(mEvent.getTrigger().friendlyDescription());

        // setup trigger click event listener
        mEditTrigger.setOnClickListener(v -> {
            EditTriggerDialog dialog = EditTriggerDialog.newInstance(mEvent.getTrigger());
            dialog.show(getFragmentManager(), "EditTriggerDialog");
            dialog.setOnClickListener(trigger -> {
                mEvent.setTrigger(trigger);
                mTriggerDescription.setText(mEvent.getTrigger().friendlyDescription());
            });
        });

        // setup action list view
        mActionListAdapter = new AutomationFragment.ActionListAdapter(getFragmentManager(), mEvent.getActions());
        mActionListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mActionListView.setAdapter(mActionListAdapter);

        // setup action click event listener
        mEditAction.setOnClickListener(v -> {
            ChooseActionDialog dialog = ChooseActionDialog.newInstance();
            dialog.show(getFragmentManager(), "ChooseActionDialog");
            dialog.setOnClickListener(newActionObject -> {
                mEvent.addAction(newActionObject);
                mActionListAdapter.notifyDataSetChanged();
            });
        });


        return view;
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @OnClick(R.id.ok)
    public void onButtonOk(View view) {
        String title = mEditEventTitle.getText().toString();
        if (title.isEmpty()) return;

        mEvent.setTitle(title);

        final AutomationPlugin plugin = AutomationPlugin.getPlugin();
        plugin.getAutomationEvents().add(mEvent);

        dismiss();
    }

    @OnClick(R.id.cancel)
    public void onButtonCancel(View view) {
        dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString("event", mEvent.toJSON());
    }

}
