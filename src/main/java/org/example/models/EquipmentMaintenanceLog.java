package org.example.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "equipmentMaintenanceLogs")
public class EquipmentMaintenanceLog {

    @Id
    private String id;

    private Integer equipmentId;
    private LocalDateTime maintenanceDate;
    private String type;
    private String description;
    private String technician;
    private String status;

    public EquipmentMaintenanceLog() {}

    public EquipmentMaintenanceLog(Integer equipmentId, LocalDateTime maintenanceDate, String type, String description, String technician, String status) {
        this.equipmentId = equipmentId;
        this.maintenanceDate = maintenanceDate;
        this.type = type;
        this.description = description;
        this.technician = technician;
        this.status = status;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public void updateStatus(String newStatus) {
        if (newStatus != null && !newStatus.trim().isEmpty()) {
            this.status = newStatus;
        }
    }

    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }

    public boolean isInProgress() {
        return "In Progress".equalsIgnoreCase(status);
    }
}
