import { Priority } from "../../enum/priority.enum"
import { Status } from "../../enum/status.enum"

export interface BreakageDto {
    id: String,
    departmentId: String,
    departmentName: String,
    room: String,
    breakageTopic: String,
    breakageText: String,
    status: Status,
    priority: Priority,
    breakageExecutor: String,
    executorAppointedBy: String,
    createdBy: String,
    createdDate: Date,
    lastUpdatedBy: String,
    lastUpdatedDate: Date,
    deadline: Date
}