import { Role } from "../enum/role";
import { CreateUserDto } from "../model/user/create-user-dto";
import { UpdateUserDto } from "../model/user/update-user-dto";

export class UserDtoFactory {

    public static createEmptyUserDto(): UpdateUserDto {
        return new UpdateUserDto('', '', Role.Employee);
    }

    public static createUserDto(username: string, password: string, departmentId: string, role: Role): CreateUserDto {
        return new CreateUserDto(username, password, departmentId, role);
    }

    public static createUpdateUserDto(username: string, departmentId: string, role: Role): UpdateUserDto {
        return new UpdateUserDto(username, departmentId, role);
    }
}