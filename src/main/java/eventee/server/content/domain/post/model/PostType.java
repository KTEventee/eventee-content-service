package eventee.server.content.domain.post.model;

import eventee.server.common.exception.BaseException;
import eventee.server.common.exception.codes.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PostType {
    TEXT("TEXT"),
    VOTE("VOTE");

    public String type;

    PostType(String type){
        this.type = type;
    }

    public static PostType from(String type) {
        return Arrays.stream(PostType.values())
                .filter(t -> t.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorCode.POST_TYPE_NOT_VALID));
    }
}
