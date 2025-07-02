import { CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Department } from '../../model/department/department';
import { DepartmentService } from '../../services/department.service';
import { DATE_FORMAT } from '../../util/constant';
import { CustomPaginatorIntl } from '../../util/custom-paginator-intl';
import { MatDialog } from '@angular/material/dialog';
import { DepartmentFormComponent } from '../../components/department-form/department-form.component';
import { ConfirmFormComponent } from '../../components/confirm-form/confirm-form.component';
import { Action } from '../../enum/action';
import { ApiResponse } from '../../model/response/api-response';
import { ApiResponseFactory } from '../../generator/api-response-factory';

@Component({
  selector: 'app-department',
  imports: [
    CommonModule,
    MatTableModule, 
    MatPaginatorModule,
    MatSort, 
    MatSortModule,
    MatInputModule,
    MatFormFieldModule
  ],
  templateUrl: './department.component.html',
  styleUrl: './department.component.css',
  providers: [
    { provide: MatPaginatorIntl, useClass: CustomPaginatorIntl }
  ]
})
export class DepartmentComponent {
  //export class DepartmentComponent implements AfterViewInit {

  number = '№';
  name = 'Отдел';
  createdBy = 'Создан';
  createdDate = 'Создано';
  lastUpdatedBy = 'Обновлен';
  lastUpdatedDate = 'Обновлено';
  actions = 'Действия';

  dateFormat = DATE_FORMAT;
  departments: any;
  getAllDepError: ApiResponse;

  public displayedColumns: string[] = [
                                    'number', 
                                    'name', 
                                    'createdBy', 
                                    'createdDate', 
                                    'lastUpdatedBy', 
                                    'lastUpdatedDate', 
                                    'actions'
                                  ];
  public dataSource!: MatTableDataSource<Department>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor (private _depService: DepartmentService, public dialog: MatDialog) {
    // this.dataSource = new MatTableDataSource(this.dep);
    this.getAllDepError = ApiResponseFactory.createEmptyApiResponse();
    this.getAllDep();
  }

  // ngAfterViewInit() {
  //   this.dataSource.paginator = this.paginator;
  //   this.dataSource.sort = this.sort;
  // }


  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  createDep() {
    const openDialog = this.dialog.open(DepartmentFormComponent, {data: {
      action: Action.Create
    }});

    openDialog.afterClosed().subscribe(() => {
      this.getAllDep();
    });
  };

  updateDep(id: string, name: string): void {
    const openDialog = this.dialog.open(DepartmentFormComponent, {data: {
      action: Action.Update,
      departmentId: id,
      departmentName: name
    }});

    openDialog.afterClosed().subscribe(() => {
      this.getAllDep();
    });
  };

  getAllDep(): void {
    this._depService
      .getAllDep()
        .subscribe({
          next: data => {
            this.departments = data;
            this.dataSource = new MatTableDataSource(this.departments);
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
          },
          error: err => {
            this.getAllDepError = err.error;
          }
        })
  };

  deleteDep(id: string, name: string): void {
    const openDialog = this.dialog.open(ConfirmFormComponent, {data: { 
      name: name 
    }});

    openDialog.afterClosed().subscribe(
      (confirmResult ) => {
        if (confirmResult) {
          this._depService.deleteDep(id)
            .subscribe({
              next: () => {
                this.getAllDep();
              },
              error: () => {
                this.getAllDep();
              }
            });
        };
    });
  };
}
