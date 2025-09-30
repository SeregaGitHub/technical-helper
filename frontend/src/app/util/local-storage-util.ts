import { Executor } from "../enum/executor.enum";

export class LocalStorageUtil {

    public static stringToBoolean(str: string | null): boolean {

        if (str != null) {
            return JSON.parse(str.toLowerCase());
        } else {
            return false;
        }
    }

    public static booleanToString(bln: boolean): string {

        return bln.toString();
    }

    public static stringToNumber(str: string | null): number {

        if (str != null) {
            return JSON.parse(str);
        } else {
            return 0;
        }
    }

    public static numberToString(n: number): string {

        return n.toString();
    }

    public static toParamString(n: string | null): string {

        if (n != null) {
            return n.toString();
        } else {
            return '';
        }
    }

    public static stringToExecutor(n: string | null): Executor {

        if (n != null) {
            switch(n) {
                case 'APPOINTED_TO_ME':
                    return Executor.AppointedToMe;
                    break;
                case 'APPOINTED_TO_OTHERS':
                    return Executor.AppointedToOthers;
                    break;
                case 'NO_APPOINTED':
                    return Executor.NoAppointed;
                    break;
                default:
                    return Executor.All;  
            }
        } else {
            return Executor.All;
        }
    }
}