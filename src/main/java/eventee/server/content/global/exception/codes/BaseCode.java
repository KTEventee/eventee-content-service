package eventee.server.content.global.exception.codes;


import eventee.server.content.global.exception.codes.reason.Reason;

public interface BaseCode {
    public Reason.ReasonDto getReasonHttpStatus();
}
