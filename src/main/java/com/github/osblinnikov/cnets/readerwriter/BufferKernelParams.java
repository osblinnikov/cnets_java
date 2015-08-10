package com.github.osblinnikov.cnets.readerwriter;

public class BufferKernelParams {
    private Object additionalData;
    private long grid_id;
    private int internalId = 0;
    private ReaderWriterInterface target;

    public BufferKernelParams(){}

    public BufferKernelParams(ReaderWriterInterface target, long grid_id, Object additionalData) {
        this.target = target;
        this.grid_id = grid_id;
        this.additionalData = additionalData;
    }

    public Object getAdditionalData(){
        return additionalData;
    }

    public void setAdditionalData(Object additionalData){
        this.additionalData = additionalData;
    }

    public long getGrid_id() {
        return grid_id;
    }

    public void setGrid_id(long grid_id) {
        this.grid_id = grid_id;
    }

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public ReaderWriterInterface getTarget() {
        return target;
    }

    public void setTarget(ReaderWriterInterface target) {
        this.target = target;
    }

    public BufferKernelParams copy(){
        return new BufferKernelParams(this.target, this.grid_id, this.additionalData);
    }
}
