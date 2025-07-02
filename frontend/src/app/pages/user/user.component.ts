import { Component, ViewChild } from '@angular/core';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule } from '@angular/material/paginator';
import { CustomPaginatorIntl } from '../../util/custom-paginator-intl';
import { DATE_FORMAT } from '../../util/constant';
import { ApiResponse } from '../../model/response/api-response';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { User } from '../../model/user/user';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { UserService } from '../../services/user.service';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { Action } from '../../enum/action';
import { ConfirmFormComponent } from '../../components/confirm-form/confirm-form.component';
import { UserFormComponent } from '../../components/user-form/user-form.component';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Role } from '../../enum/role';

@Component({
  selector: 'app-user',
  imports: [
    CommonModule,
    MatTableModule, 
    MatPaginatorModule,
    MatSort, 
    MatSortModule,
    MatInputModule,
    MatFormFieldModule
  ],
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

  createUser() {
      const openDialog = this.dialog.open(UserFormComponent, {data: {
        action: Action.Create
      }});
  
      openDialog.afterClosed().subscribe(() => {
        this.getAllUsers();
      });
  };

  updateUser(userId: string, username: string, departmentId: string, role: Role): void {
      const openDialog = this.dialog.open(UserFormComponent, {data: {
        action: Action.Update,
        userId: userId,
        username: username,
        departmentId: departmentId,
        role: role
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

  deleteUser(id: string, username: string): void {
      const openDialog = this.dialog.open(ConfirmFormComponent, {data: { 
        name: username 
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
