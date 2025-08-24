import { Component, ViewChild } from '@angular/core';
import { BreakageService } from '../../services/breakage.service';
import { Breakage } from '../../model/breakage/breakage';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { DATE_FORMAT } from '../../util/constant';
import { ApiResponse } from '../../model/response/api-response';
import { ApiResponseFactory } from '../../generator/api-response-factory';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSelectModule } from '@angular/material/select';
import { Executor } from '../../enum/executor.enum';
import { MatDialog } from '@angular/material/dialog';
import { BreakageFormComponent } from '../../components/breakage-form/breakage-form.component';
import { EnumView } from '../../util/enum-view';

@Component({
  selector: 'app-breakage',
  imports: [
    MatSidenavModule,
    MatIconModule,
    FormsModule,
    CommonModule,
    MatTableModule, 
    MatPaginatorModule,
    MatSort, 
    MatSortModule,
    MatInputModule,
    MatFormFieldModule,
    MatSlideToggleModule,
    MatSelectModule
  ],
  templateUrl: './breakage.component.html',
  styleUrl: './breakage.component.css'
})
export class BreakageComponent {

  opened = false;

  executors: EnumView[] = [
    {value: Executor.All, viewValue: 'Всех'},
    {value: Executor.AppointedToMe, viewValue: 'На меня'},
    {value: Executor.AppointedToOthers, viewValue: 'На других'},
    {value: Executor.NoAppointed, viewValue: 'Не назначенные'},
  ];

  number = '№';
  departmentName = 'Отдел';
  room = 'Помещение';
  breakageTopic = 'Тема';
  status = 'Статус';
  priority = 'Приоритет';
  breakageExecutor = 'Исполнитель';
  createdBy = 'Создан';
  createdDate = 'Создано';

  pageIndex = 0;
  pageSize = 10;
  sortBy = 'lastUpdatedDate';
  direction = 'DESC';
  statusNew = true;
  statusSolved = false;
  statusInProgress = true; 
  statusPaused = true; 
  statusRedirected = true; 
  statusCancelled = false; 
  priorityUrgently = true; 
  priorityHigh = true; 
  priorityMedium = true; 
  priorityLow = true;
  executor = Executor.All; 
  deadline = false;

  dateFormat = DATE_FORMAT;
  breakages: any;
  getAllBreakagesError: ApiResponse;

  public displayedColumns: string[] = [
                                    'number', 
                                    'departmentName', 
                                    'room', 
                                    'breakageTopic',
                                    'status', 
                                    'priority', 
                                    'breakageExecutor', 
                                    'createdBy',
                                    'createdDate'
                                  ];
  public dataSource!: MatTableDataSource<Breakage>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor (private _breakageService: BreakageService, public dialog: MatDialog) { 
    this.getAllBreakagesError = ApiResponseFactory.createEmptyApiResponse();
    this.getAllBreakages();
   }

  // Need for updating
  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  createBreakage() {
    
    const openDialog = this.dialog.open(BreakageFormComponent);
    
    openDialog.afterClosed().subscribe(() => {
        this.getAllBreakages();
    });
  };


  getAllBreakages(): void {
    this._breakageService
      .getAllBreakages(
        this.pageIndex, this.pageSize, this.sortBy, this.direction, 
        this.statusNew, this.statusSolved, this.statusInProgress, 
        this.statusPaused, this.statusRedirected, this.statusCancelled,
        this.priorityUrgently, this.priorityHigh, this.priorityMedium, this.priorityLow,
        this.executor, this.deadline
      )
        .subscribe({
          next: data => {
            this.breakages = data.content;
            this.dataSource = new MatTableDataSource(this.breakages);
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
          },
          error: err => {
            this.getAllBreakagesError = err.error;
          }
        })
  };

  getBreakageById(id: string) {
    console.log('getBreakageById() - Id=' + id)
  }


}
