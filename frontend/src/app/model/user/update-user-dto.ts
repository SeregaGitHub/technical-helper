import { Role } from "../../enum/role";

export interface UpdateUserDto {
    username: string;
    departmentId: string;
    role: Role;
}