package example.org.com.downloaddemo;

public interface DownLoadListener {
    public void onProgress(int progress);

    public void onSuccess();

    public void onFailed();

    public void onPaused();

    public void onCanceled();
}
