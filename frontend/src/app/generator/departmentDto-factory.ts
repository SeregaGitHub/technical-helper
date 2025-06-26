import { DepartmentDto } from "../model/departmentDto";

export class DepartmentDtoFactory {

    public static createEmptyDepartmentDto(): DepartmentDto {
        return new DepartmentDto('');
    }
}