import { Role } from "../../enum/role";
import { UpdateUserDto } from "./update-user-dto";

export class CreateUserDto extends UpdateUserDto {
    password: string;

    constructor(username: string, password: string, departmentId: string, role: Role) {
        super(username, departmentId, role);
        this.password = password;
    }
}