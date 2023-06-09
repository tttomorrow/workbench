package org.opengauss.admin.common.core.domain.model.ops;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author lhf
 * @date 2022/8/17 18:01
 **/
@Data
public class HostUserBody {
    @NotBlank(message = "The host ID cannot be empty")
    private String hostId;
    @NotBlank(message = "The user name cannot be empty")
    private String username;
    @NotBlank(message = "The password cannot be empty")
    private String password;
    private String rootPassword;
    private Boolean sudo;

    public OpsHostUserEntity toEntity(Boolean sudo) {
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setSudo(sudo);
        hostUserEntity.setHostId(hostId);
        hostUserEntity.setUsername(username);
        hostUserEntity.setPassword(password);
        return hostUserEntity;
    }
}
