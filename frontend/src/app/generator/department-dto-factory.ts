import { DepartmentDto } from "../model/department-dto";

export class DepartmentDtoFactory {

    public static createEmptyDepartmentDto(): DepartmentDto {
        return new DepartmentDto('');
    }
}