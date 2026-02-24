package com.zjut.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName notification
 */
@TableName(value ="notification")
@Data
public class Notification {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String title;

    /**
     * 通知正文内容
     */
    private String content;

    /**
     * 通知类型: 0-全局公告, 1-审批进度, 2-重要变更
     */
    private Integer type;

    /**
     * 发送者ID (系统发送可为NULL)
     */
    private Long senderId;

    /**
     * 定向接收者ID (全局公告则为NULL)
     */
    private Long targetUserId;

    /**
     * 
     */
    private LocalDateTime createTime;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Integer isRead;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Notification other = (Notification) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getSenderId() == null ? other.getSenderId() == null : this.getSenderId().equals(other.getSenderId()))
            && (this.getTargetUserId() == null ? other.getTargetUserId() == null : this.getTargetUserId().equals(other.getTargetUserId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getSenderId() == null) ? 0 : getSenderId().hashCode());
        result = prime * result + ((getTargetUserId() == null) ? 0 : getTargetUserId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", title=").append(title);
        sb.append(", content=").append(content);
        sb.append(", type=").append(type);
        sb.append(", senderId=").append(senderId);
        sb.append(", targetUserId=").append(targetUserId);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}