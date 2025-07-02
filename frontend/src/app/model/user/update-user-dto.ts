import { Role } from "../../enum/role";

export class UpdateUserDto {
    username: string;
    departmentId: string;
    role: Role;

    constructor(username: string, departmentId: string, role: Role) {
        this.username = username;
        this.departmentId = departmentId;
        this.role = role;
    }
}