package com.github.osblinnikov.cnetsjava.readerwriter;

public class BufferKernelParams {
    public int getBufferId() {
        return bufferId;
    }

    public void setBufferId(int bufferId) {
        this.bufferId = bufferId;
    }

    private int bufferId = -1;
    private long grid_id;
    private int internalId = 0;
    private ReaderWriterInterface target;
    private boolean readNested = true;
    private boolean allowedForwardCall = true;

    public BufferKernelParams(){}

    public BufferKernelParams(ReaderWriterInterface target, long gridId) {
        this.target = target;
        this.grid_id = gridId;
    }

    public BufferKernelParams(ReaderWriterInterface target, long grid_id, int bufferId) {
        this(target,grid_id);
        this.bufferId = bufferId;
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
        return new BufferKernelParams(this.target, this.grid_id, this.bufferId);
    }

    public boolean getReadNested() {
        return readNested;
    }

    public boolean isReadNested() {
        return readNested;
    }

    public void setReadNested(boolean readNested) {
        this.readNested = readNested;
    }

    public boolean isAllowedForwardCall() {
        return allowedForwardCall;
    }

    public void setAllowedForwardCall(boolean allowedForwardCall) {
        this.allowedForwardCall = allowedForwardCall;
    }
}
