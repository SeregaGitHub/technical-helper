import { Component, ViewChild } from '@angular/core';
import { MatPaginator, MatPaginatorIntl } from '@angular/material/paginator';
import { CustomPaginatorIntl } from '../../util/custom-paginator-intl';
import { DATE_FORMAT } from '../../util/constant';
import { ApiResponse } from '../../model/response/api-response';
import { MatTableDataSource } from '@angular/material/table';
import { User } from '../../model/user/user';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { UserService } from '../../services/user.service';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { Action } from '../../enum/action';
import { ConfirmFormComponent } from '../../components/confirm-form/confirm-form.component';
import { UserFormComponent } from '../../components/user-form/user-form.component';

@Component({
  selector: 'app-user',
  imports: [],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css',
  providers: [
    { provide: MatPaginatorIntl, useClass: CustomPaginatorIntl }
  ]
})
export class UserComponent {

  number = '№';
  username = 'Логин';
  department = 'Отдел';
  role = 'Роль';
  createdBy = 'Создан';
  createdDate = 'Создано';
  lastUpdatedBy = 'Обновлен';
  lastUpdatedDate = 'Обновлено';
  actions = 'Действия';
  
  dateFormat = DATE_FORMAT;
  users: any;
  getAllUsersError: ApiResponse;

  public displayedColumns: string[] = [
                                    'number', 
                                    'username', 
                                    'department', 
                                    'role',
                                    'createdBy', 
                                    'createdDate', 
                                    'lastUpdatedBy', 
                                    'lastUpdatedDate', 
                                    'actions'
                                  ];
  public dataSource!: MatTableDataSource<User>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor (private _userService: UserService, public dialog: MatDialog) {
    this.getAllUsersError = ApiResponseFactory.createEmptyApiResponse();
    this.getAllUsers();
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  createDep() {
      const openDialog = this.dialog.open(UserFormComponent, {data: {
        action: Action.Create
      }});
  
      openDialog.afterClosed().subscribe(() => {
        this.getAllUsers();
      });
  };

  updateDep(id: string, name: string): void {
      const openDialog = this.dialog.open(UserFormComponent, {data: {
        action: Action.Update,
        departmentId: id,
        departmentName: name
      }});
  
      openDialog.afterClosed().subscribe(() => {
        this.getAllUsers();
      });
  };

  getAllUsers(): void {
    this._userService
      .getAllUsers()
        .subscribe({
          next: data => {
            this.users = data;
            this.dataSource = new MatTableDataSource(this.users);
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
          },
          error: err => {
            this.getAllUsersError = err.error;
          }
        })
  };

  deleteDep(id: string, name: string): void {
      const openDialog = this.dialog.open(ConfirmFormComponent, {data: { 
        username: name 
      }});
  
      openDialog.afterClosed().subscribe(
        (confirmResult ) => {
          if (confirmResult) {
            this._userService.deleteUser(id)
              .subscribe({
                next: () => {
                  this.getAllUsers();
                },
                error: () => {
                  this.getAllUsers();
                }
              });
          };
      });
  };
}
