import { Role } from "../../enum/role.enum";

export interface User {
    id: number;
    username: string;
    department: string;
    role: Role;
    createdBy: string;
    createdDate: Date;
    lastUpdatedBy: string;
    lastUpdatedDate: Date;
}