import { Executor } from "../enum/executor.enum";
import { Priority } from "../enum/priority.enum";
import { Status } from "../enum/status.enum";
import { ExecutorView } from "../util/executor-view";
import { PriorityView } from "../util/priority-view";

export class EnumViewFactory {

    public static getExecutorViews(): ExecutorView[] {

        const executors = [
            {value: Executor.All, viewValue: 'Все заявки'},
            {value: Executor.AppointedToMe, viewValue: 'На меня'},
            {value: Executor.AppointedToOthers, viewValue: 'На других'},
            {value: Executor.NoAppointed, viewValue: 'Не назначенные'},
          ];
        
        return executors;
    };

    public static getPriorities(): PriorityView[] {

        const priorities = [
            {value: Priority.Urgently, viewValue: 'Важный'},
            {value: Priority.High, viewValue: 'Высокий'},
            {value: Priority.Medium, viewValue: 'Средний'},
            {value: Priority.Low, viewValue: 'Низкий'}
        ];

        return priorities;
    }

    public static getStatusViews(): Map<Status, string> {

        const statusMap = new Map<Status, string>([ 
            [Status.New, 'Новая'],
            [Status.Solved, 'Решена'],
            [Status.InProgress, 'В работе'],
            [Status.Paused, 'В ожидании'],
            [Status.Redirected, 'Передана'],
            [Status.Cancelled, 'Отменена']
        ]);

        return statusMap;
    };

    public static getPriorityViews(): Map<Priority, string> {

        const priorityMap = new Map<Priority, string>([ 
            [Priority.Urgently, 'Важный'],
            [Priority.High, 'Высокий'],
            [Priority.Medium, 'Средний'],
            [Priority.Low, 'Низкий']
        ]);

        return priorityMap;
    };
}