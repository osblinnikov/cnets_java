package com.github.osblinnikov.cnets.types;

public class bufferReadData {
    long nested_buffer_id = 0;
    long writer_grid_id = 0;
    Object data = null;

  public long getNested_buffer_id() {
        return nested_buffer_id;
    }

    public void setNested_buffer_id(long nested_buffer_id) {
        this.nested_buffer_id = nested_buffer_id;
    }

    public long getWriter_grid_id() {
        return writer_grid_id;
    }

    public void setWriter_grid_id(long writer_grid_id) {
        this.writer_grid_id = writer_grid_id;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
