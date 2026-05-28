package emplay.entertainment.emplay.fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import emplay.entertainment.emplay.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Base class for fragments to provide common lifecycle-safe utilities.
 */
public abstract class BaseFragment extends Fragment {

    /**
     * Enqueues a Retrofit call and only triggers the callback if the fragment is still added.
     */
    protected <T> void safeEnqueue(Call<T> call, Callback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (isAdded()) {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                if (isAdded()) {
                    callback.onFailure(call, t);
                }
            }
        });
    }

    /**
     * Safely runs a runnable on the UI thread if the fragment is still added.
     */
    protected void safeRunOnUiThread(Runnable runnable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    runnable.run();
                }
            });
        }
    }

    /**
     * Common navigation logic to replace fragments in the main container.
     */
    protected void navigateTo(Fragment fragment) {
        if (!isAdded()) return;
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
