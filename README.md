# booksource
用到的的知识 AsyncTask, OkHttp,Service,Activity,Notification,运行时权限 
1.编写接口 DownLoadListener{
  onProgress(int progress);
  onSuccess();
  onFail();
  onPause();
  onCancel();
}
2.编写下载任务类Task 
DownloadTask extends AsyncTask<String,Integer,Integer>{
  自定四个状态
  1.成功 TYPE_SUCCESS;
  2.失败 TYPE_FAIL;
  3.取消 TYPE_CACEL;
  4.暂停 TYPE_PAUSE;
  
  set DownLoadListener;
  
   boolean isPause,isCancel;
  
  线程中执行下载任务 doInBackground
					1.url 获取fileName 2.downLoadContentLength获取已下载的文件大小(与网络的文件（contentLength）判断是否相等,返回状态) 3.通过okHttp来获取资源
					4.RandomAccessFile 继续写入 4.计算下载的百分比 更新进度条
  更新UI进度条       onProgressUpdate 通过DownLoadListener 将进度传递出去
  返回状态通知UI     onPostExecute 根据返回的四种状态 调用DownLoadListener
  
}
3.编写前台service 
   1.实现DownLoadListener 接口 
   2.通过Notification 通知用户下载内容
   3.编写binder,给acitivty调用 startDownload(String url); pauseDownLoad();cancelDownLoad()--->记住要删除下载的文件;
   4.Activity 与service 通过binder对象绑定
   
4.Activity 启动startService，并且绑定Service
