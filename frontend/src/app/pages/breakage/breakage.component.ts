import { Component, ViewChild } from '@angular/core';
import { BreakageService } from '../../services/breakage.service';
import { Breakage } from '../../model/breakage/breakage';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorIntl, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
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
import { EnumViewFactory } from '../../generator/enum-view-factory';
import { CustomBreakagePaginatorIntl } from '../../util/custom-breakage-paginator-intl';

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
  styleUrl: './breakage.component.css',
  providers: [
    { provide: MatPaginatorIntl, useClass: CustomBreakagePaginatorIntl }
  ]
})
export class BreakageComponent {

  opened = false;

  executors = EnumViewFactory.getExecutorViews();

  statusMap = EnumViewFactory.getStatusViews();

  priorityMap = EnumViewFactory.getPriorityViews();

  number = '№';
  departmentName = 'Отдел';
  room = 'Помещение';
  breakageTopic = 'Тема';
  status = 'Статус';
  priority = 'Приоритет';
  breakageExecutor = 'Исполнитель';
  createdBy = 'Создан';
  createdDate = 'Создано';

  // pageIndex = 0;
  //pageSize = 10;
  totalElements: number = 0;
  pageEvent!: PageEvent;

  sortBy = 'lastUpdatedDate';
  direction = 'DESC';

  statusNew = true;
  statusSolved = true;
  statusInProgress = true; 
  statusPaused = true; 
  statusRedirected = true; 
  statusCancelled = true; 
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
    console.log('constructor');
    this.getAllBreakages(0, 10);
   }

  // Need for updating
  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

// https://yandex.ru/video/preview/10275455143653628593    paginator

  onPaginateChange(event: PageEvent) {
    console.log('event:');
    console.log(event);
    console.log(event.pageIndex);
    console.log(event.pageSize);
    console.log('event:');

    // this.pageIndex = event.pageIndex;
    // this.pageSize = event.pageSize;
    // this.totalElements = event.length;

    //this.pageIndex = this.pageIndex + 1;
    this.getAllBreakages(event.pageIndex, event.pageSize);
  }

  createBreakage() {
    
    const openDialog = this.dialog.open(BreakageFormComponent);
    
    openDialog.afterClosed().subscribe(() => {
        this.getAllBreakages(0, 10);
    });
  };


  getAllBreakages(pageIndex: number, pageSize: number): void {

    //console.log('getAllBreakages() start: this.paginator.length - ' + this.paginator.length);

    this._breakageService
      .getAllBreakages(
        pageIndex, pageSize, this.sortBy, this.direction, 
        this.statusNew, this.statusSolved, this.statusInProgress, 
        this.statusPaused, this.statusRedirected, this.statusCancelled,
        this.priorityUrgently, this.priorityHigh, this.priorityMedium, this.priorityLow,
        this.executor, this.deadline
      )
        .subscribe({
          next: data => {
            this.breakages = data.content;
            this.dataSource = new MatTableDataSource(this.breakages);
            //this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;

            this.totalElements = data.totalElements;
            console.log(data.totalElements)
            console.log(data.pageNumber)
            console.log(data.pageSize)
            // this.pageIndex = data.pageNumber;
            //this.pageSize = data.pageSize;

            // console.log('this.totalElements - ');
            // console.log(this.totalElements)
            // console.log('this.pageIndex - ');
            // console.log(this.pageIndex)
            // console.log('this.pageSize - ');
            // console.log(this.pageSize)

          },
          error: err => {
            this.getAllBreakagesError = err.error;
          }
        })

        //console.log('getAllBreakages() finish: this.paginator.length - ' + this.paginator.length);
  };

  getBreakageById(id: string) {
    console.log('getBreakageById() - Id=' + id)
  }


}
