package com.logansoft.UIEngine.utils.http.multipart;

import java.io.IOException;
import java.io.OutputStream;

import com.android.internal.http.multipart.Part;

public class LoganSoftMultipartEntity extends  com.android.internal.http.multipart.MultipartEntity {
    private OutputStreamProgress outstream;

	public LoganSoftMultipartEntity(Part[] parts) {
		super(parts);
		// TODO Auto-generated constructor stub
	}
	@Override
    public void writeTo(OutputStream outstream) throws IOException {
        this.outstream = new OutputStreamProgress(outstream);
        this.outstream.totalLength=getContentLength();
        super.writeTo(this.outstream);
    }

  
    public int getProgress() {
        if (outstream == null) {
            return 0;
        }
        long contentLength = getContentLength();
        if (contentLength <= 0) { // Prevent division by zero and negative values
            return 0;
        }
        long writtenLength = outstream.getWrittenLength();
        return (int) (100*writtenLength/contentLength);
    }

}
