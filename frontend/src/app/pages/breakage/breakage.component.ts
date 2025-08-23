import { Component, ViewChild } from '@angular/core';
import { BreakageService } from '../../services/breakage.service';
import { Breakage } from '../../model/breakage/breakage';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { DATE_FORMAT } from '../../util/constant';
import { ApiResponse } from '../../model/response/api-response';
import { ApiResponseFactory } from '../../generator/api-response-factory';

@Component({
  selector: 'app-breakage',
  imports: [],
  templateUrl: './breakage.component.html',
  styleUrl: './breakage.component.css'
})
export class BreakageComponent {

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
  executor = null;
  deadline = false;

  dateFormat = DATE_FORMAT;
  breakages: any;
  getAllBreakagesError?: ApiResponse;

  public dataSource!: MatTableDataSource<Breakage>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor (private _breakageService: BreakageService) { 
    this.getAllBreakagesError = ApiResponseFactory.createEmptyApiResponse();
    this.getAllBreakages();
   }


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
            this.breakages = data;
            this.dataSource = new MatTableDataSource(this.breakages);
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;

            console.log(this.breakages);
          },
          error: err => {
            this.getAllBreakagesError = err.error;
          }
        })
  };


}
