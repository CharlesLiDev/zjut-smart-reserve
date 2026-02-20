package com.zjut.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 预约申请表
 * @TableName booking_record
 */
@TableName(value ="booking_record")
@Data
public class BookingRecord {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请人
     */
    private Long userId;

    /**
     * 预约场地
     */
    private Long venueId;

    /**
     * 活动名称
     */
    private String eventName;

    /**
     * 主办单位
     */
    private String hostUnit;

    /**
     * 预计人数
     */
    private Integer exceptNum;

    /**
     * 
     */
    private String contactPerson;

    /**
     * 
     */
    private String contactPhone;

    /**
     * 活动简要说明
     */
    private String description;

    /**
     * 策划书地址
     */
    private String planDocUrl;

    /**
     * 使用日期
     */
    private LocalDate bookingDate;

    /**
     * 时间段
     */
    private String timeSlot;

    /**
     * 0:待审核, 1:已驳回, 2:已通过, 3:已使用, 4:已取消
     */
    private Integer status;

    /**
     * 驳回理由
     */
    private String rejectReason;

    /**
     * 
     */
    private LocalDateTime createTime;

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
        BookingRecord other = (BookingRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getVenueId() == null ? other.getVenueId() == null : this.getVenueId().equals(other.getVenueId()))
            && (this.getEventName() == null ? other.getEventName() == null : this.getEventName().equals(other.getEventName()))
            && (this.getHostUnit() == null ? other.getHostUnit() == null : this.getHostUnit().equals(other.getHostUnit()))
            && (this.getExceptNum() == null ? other.getExceptNum() == null : this.getExceptNum().equals(other.getExceptNum()))
            && (this.getContactPerson() == null ? other.getContactPerson() == null : this.getContactPerson().equals(other.getContactPerson()))
            && (this.getContactPhone() == null ? other.getContactPhone() == null : this.getContactPhone().equals(other.getContactPhone()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getPlanDocUrl() == null ? other.getPlanDocUrl() == null : this.getPlanDocUrl().equals(other.getPlanDocUrl()))
            && (this.getBookingDate() == null ? other.getBookingDate() == null : this.getBookingDate().equals(other.getBookingDate()))
            && (this.getTimeSlot() == null ? other.getTimeSlot() == null : this.getTimeSlot().equals(other.getTimeSlot()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getRejectReason() == null ? other.getRejectReason() == null : this.getRejectReason().equals(other.getRejectReason()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getVenueId() == null) ? 0 : getVenueId().hashCode());
        result = prime * result + ((getEventName() == null) ? 0 : getEventName().hashCode());
        result = prime * result + ((getHostUnit() == null) ? 0 : getHostUnit().hashCode());
        result = prime * result + ((getExceptNum() == null) ? 0 : getExceptNum().hashCode());
        result = prime * result + ((getContactPerson() == null) ? 0 : getContactPerson().hashCode());
        result = prime * result + ((getContactPhone() == null) ? 0 : getContactPhone().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getPlanDocUrl() == null) ? 0 : getPlanDocUrl().hashCode());
        result = prime * result + ((getBookingDate() == null) ? 0 : getBookingDate().hashCode());
        result = prime * result + ((getTimeSlot() == null) ? 0 : getTimeSlot().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getRejectReason() == null) ? 0 : getRejectReason().hashCode());
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
        sb.append(", userId=").append(userId);
        sb.append(", venueId=").append(venueId);
        sb.append(", eventName=").append(eventName);
        sb.append(", hostUnit=").append(hostUnit);
        sb.append(", exceptNum=").append(exceptNum);
        sb.append(", contactPerson=").append(contactPerson);
        sb.append(", contactPhone=").append(contactPhone);
        sb.append(", description=").append(description);
        sb.append(", planDocUrl=").append(planDocUrl);
        sb.append(", bookingDate=").append(bookingDate);
        sb.append(", timeSlot=").append(timeSlot);
        sb.append(", status=").append(status);
        sb.append(", rejectReason=").append(rejectReason);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}