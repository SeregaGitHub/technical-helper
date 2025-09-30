import { CommonModule } from '@angular/common';
import { Component, OnDestroy, ViewChild } from '@angular/core';
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
import { Action } from '../../enum/action.enum';
import { ApiResponse } from '../../model/response/api-response';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { ViewDepartmentFormComponent } from '../../components/view-department-form/view-department-form.component';
import { Subject, takeUntil } from 'rxjs';

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
export class DepartmentComponent implements OnDestroy {
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
  department!: Department;
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

  private _unsubscribe: Subject<void> = new Subject();

  constructor (private _depService: DepartmentService, public dialog: MatDialog) {
    // this.dataSource = new MatTableDataSource(this.dep);
    this.getAllDepError = ApiResponseFactory.createEmptyApiResponse();
    this.getAllDep();
  }

  ngOnDestroy(): void {
    this._unsubscribe.next();
    this._unsubscribe.complete();
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

    openDialog.afterClosed()
      .pipe(takeUntil(this._unsubscribe))
        .subscribe(() => {
          this.getAllDep();
        });
  };

  updateDep(id: string, name: string): void {
    const openDialog = this.dialog.open(DepartmentFormComponent, {data: {
      action: Action.Update,
      departmentId: id,
      departmentName: name
    }});

    openDialog.afterClosed()
      .pipe(takeUntil(this._unsubscribe))
        .subscribe(() => {
          this.getAllDep();
        });
  };

  getAllDep(): void {
    this._depService
      .getAllDep()
        .pipe(takeUntil(this._unsubscribe))
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

  getDepById(id: string) {
    this._depService
      .getDepById(id)
        .pipe(takeUntil(this._unsubscribe))
          .subscribe({
            next: data => {
              this.department = data;

              this.dialog.open(ViewDepartmentFormComponent, {data: {
                departmentId: this.department.id,
                departmentName: this.department.name,
                createdBy: this.department.createdBy,
                createdDate: this.department.createdDate,
                lastUpdatedBy: this.department.lastUpdatedBy,
                lastUpdatedDate: this.department.lastUpdatedDate
              }});
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

    openDialog.afterClosed()
      .pipe(takeUntil(this._unsubscribe))
        .subscribe(
          (confirmResult ) => {
            if (confirmResult) {
              this._depService.deleteDep(id)
                .pipe(takeUntil(this._unsubscribe))
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
