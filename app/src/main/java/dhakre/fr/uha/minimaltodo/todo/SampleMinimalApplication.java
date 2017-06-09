package dhakre.fr.uha.minimaltodo.todo;

import com.sumup.merchant.api.SumUpState;

/**
 * Created by user on 07-Jun-17.
 */

public class SampleMinimalApplication extends AnalyticsApplication{
    @Override
    public void onCreate() {
        super.onCreate();
        SumUpState.init(this);
    }
}
