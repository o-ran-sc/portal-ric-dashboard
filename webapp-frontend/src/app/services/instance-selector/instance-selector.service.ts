/*-
 * ========================LICENSE_START=================================
 * O-RAN-SC
 * %%
 * Copyright (C) 2019 AT&T Intellectual Property
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================LICENSE_END===================================
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { RicInstance } from '../../interfaces/dashboard.types';
import { finalize } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class InstanceSelectorService {

  private selectedInstance: BehaviorSubject<RicInstance> ;
  private basePath = 'api/admin/instance';
  private instanceList: RicInstance[];
  private isloadingFinish = new BehaviorSubject<boolean>(false);

  constructor(private httpClient: HttpClient) {
    //will call backend and load the instance list
    //this call will only invoked once
    this.pullInstanceList()
      .pipe(
        finalize(() => this.isloadingFinish.next(true))
      )
      .subscribe((instanceList: RicInstance[]) => {
      console.log(instanceList)
      this.instanceList = instanceList
      this.selectedInstance = new BehaviorSubject<RicInstance>(instanceList[0])
    })
  }

  pullInstanceList(): Observable<RicInstance[]> {
    return this.httpClient.get<RicInstance[]>(this.basePath);
  }

  getInstanceLoading() {
    return this.isloadingFinish;
  }

  getInstanceList() {
    return this.instanceList;
  }

  getSelectedInstance(): BehaviorSubject<RicInstance> {
    return this.selectedInstance;
  }

  getBasePath() {
    var basePath = 'api/ric/' + this.selectedInstance.value.key
    return basePath
  }

  updateSelectedInstance(instance: RicInstance) {
    this.selectedInstance.next(instance)
  }

}
