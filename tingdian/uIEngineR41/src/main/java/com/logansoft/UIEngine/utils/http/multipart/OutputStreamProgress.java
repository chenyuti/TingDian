package com.logansoft.UIEngine.utils.http.multipart;

import java.io.IOException;
import java.io.OutputStream;

import com.logansoft.UIEngine.utils.ProgressDialogUtil;

public class OutputStreamProgress extends OutputStream {
	private final OutputStream outstream;
    private volatile long bytesWritten=0;
    public long totalLength;
    public OutputStreamProgress(OutputStream outstream) {
        this.outstream = outstream;
    }

    @Override
    public void write(int b) throws IOException {
        outstream.write(b);
        bytesWritten++;
        refresh();
    }

    @Override
    public void write(byte[] b) throws IOException {
        outstream.write(b);
        bytesWritten += b.length;
        refresh();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outstream.write(b, off, len);
        bytesWritten += len;
        refresh();
    }

    @Override
    public void flush() throws IOException {
        outstream.flush();
    }

    @Override
    public void close() throws IOException {
        outstream.close();
    }

    public long getWrittenLength() {
        return bytesWritten;
    }
    public void refresh(){
    	if(bytesWritten<totalLength){
    		ProgressDialogUtil.setMessage(bytesWritten/1024 + "kb / " + totalLength/1024 + "kb");
    	}
    	else
    		ProgressDialogUtil.setMessage("提交成功，请稍等...");
    		
    }
}
