package eventee.server.content.global.exception;
import eventee.server.content.global.exception.codes.BaseCode;
import eventee.server.content.global.exception.codes.reason.Reason;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException {

    private BaseCode code;

    public Reason.ReasonDto getErrorReasonHttpStatus(){
        return this.code.getReasonHttpStatus();
    }
}