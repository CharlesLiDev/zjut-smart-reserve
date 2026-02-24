package com.zjut.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName notification_read
 */
@TableName(value ="notification_read")
@Data
public class NotificationRead {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联者的通知id
     */
    private Long notificationId;

    /**
     * 
     */
    private Long userId;

    /**
     * 
     */
    private LocalDateTime readTime;

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
        NotificationRead other = (NotificationRead) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getNotificationId() == null ? other.getNotificationId() == null : this.getNotificationId().equals(other.getNotificationId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getReadTime() == null ? other.getReadTime() == null : this.getReadTime().equals(other.getReadTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getNotificationId() == null) ? 0 : getNotificationId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getReadTime() == null) ? 0 : getReadTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", notificationId=").append(notificationId);
        sb.append(", userId=").append(userId);
        sb.append(", readTime=").append(readTime);
        sb.append("]");
        return sb.toString();
    }
}