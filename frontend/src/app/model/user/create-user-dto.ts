import { Role } from "../enum/role";

export interface CreateUserDto {
    username: string;
    password: string;
    departmentId: string;
    role: Role;
}