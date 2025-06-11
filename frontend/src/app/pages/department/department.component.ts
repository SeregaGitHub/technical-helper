import { CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule } from '@angular/material/paginator';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Department } from '../../model/department';
import { DepartmentService } from '../../services/department.service';
import { DATE_FORMAT } from '../../util/constant';
import { CustomPaginatorIntl } from '../../util/customPaginatorIntl';

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
  id = "Id"
  name = 'Отдел';
  createdBy = 'Создан';
  createdDate = 'Дата';
  lastUpdatedBy = 'Обновлен';
  lastUpdatedDate = 'Обновлено';
  actions = 'Действия';

  dateFormat = DATE_FORMAT;
  departments: any;

  public displayedColumns: string[] = ['number', 'name', 'createdBy', 'createdDate', 'lastUpdatedBy', 'lastUpdatedDate', 'actions'];
  public dataSource!: MatTableDataSource<Department>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor (private _depService: DepartmentService) {
    // this.dataSource = new MatTableDataSource(this.dep);
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

  getAllDep(): void {
    this._depService
      .getAllDep()
        .subscribe(data => {

          console.log(data);

          this.departments = data;
          this.dataSource = new MatTableDataSource(this.departments);

          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
        });
  }

  updateDep(name: String): void {
    console.log("Update Department - " + name)
  }

  deleteDep(id: string): void {
    this._depService.deleteDep(id)
      .subscribe(str => {
        console.log(str);
        this.getAllDep();
      });
  }

  // getAllDep(): void {
  //   this._depService
  //     .getAllDep()
  //       .subscribe(data => {
  //         this.dataSource = new MatTableDataSource(data)
  //       });
  //     console.log("getAllDep() - " + this.dep);
  // }

  onCreate() {
    console.log("Crearte new Department...")
  }
}
