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

import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { ACAdmissionIntervalControl, ACAdmissionIntervalControlAck } from '../interfaces/ac-xapp.types';
import { ACXappService } from '../services/ac-xapp/ac-xapp.service';
import { ErrorDialogService } from '../services/ui/error-dialog.service';
import { NotificationService } from './../services/ui/notification.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'rd-ac-xapp',
  templateUrl: './ac-xapp.component.html',
  styleUrls: ['./ac-xapp.component.scss']
})
export class AcXappComponent implements OnInit {

  private acForm: FormGroup;

  constructor(
    private acXappService: ACXappService,
    private errorDialogService: ErrorDialogService,
    private notificationService: NotificationService) { }

  ngOnInit() {
    const windowLengthPattern = /^([0-9]{1}|[1-5][0-9]{1}|60)$/;
    const blockingRatePattern = /^([0-9]{1,2}|100)$/;
    const triggerPattern = /^([0-9]+)$/;
    this.acForm = new FormGroup({
      // Names must match the ACAdmissionIntervalControl interface
      enforce: new FormControl(true, [Validators.required]),
      window_length: new FormControl('', [Validators.required, Validators.pattern(windowLengthPattern)]),
      blocking_rate: new FormControl('', [Validators.required, Validators.pattern(blockingRatePattern)]),
      trigger_threshold: new FormControl('', [Validators.required, Validators.pattern(triggerPattern)])
    });
    // TODO: show pending action indicator
    this.acXappService.getPolicy().subscribe((res: ACAdmissionIntervalControl) => {
      this.acForm.controls['enforce'].setValue(res.enforce);
      this.acForm.controls['window_length'].setValue(res.window_length);
      this.acForm.controls['blocking_rate'].setValue(res.blocking_rate);
      this.acForm.controls['trigger_threshold'].setValue(res.trigger_threshold);
      // TODO: clear pending action indicator
    },
      (error: HttpErrorResponse) => {
        // TODO: clear pending action indicator
        this.errorDialogService.displayError(error.message);
      });
  }

  updateAc = (acFormValue: ACAdmissionIntervalControl) => {
    if (this.acForm.valid) {
      // convert strings to numbers using the plus operator
      const acFormValueConverted = {
        enforce: acFormValue.enforce,
        window_length: +acFormValue.window_length,
        blocking_rate: +acFormValue.blocking_rate,
        trigger_threshold: +acFormValue.trigger_threshold
      };
      this.acXappService.putPolicy(acFormValueConverted).subscribe(
        response => {
          if (response.status === 200) {
            this.notificationService.success('AC update policy succeeded!');
          }
        },
        (error => {
          this.errorDialogService.displayError('AC update policy failed: ' + error.message);
        })
      );
    }
  }

  hasError(controlName: string, errorName: string) {
    if (this.acForm.controls[controlName].hasError(errorName)) {
      return true;
    }
    return false;
  }

  validateControl(controlName: string) {
    if (this.acForm.controls[controlName].invalid && this.acForm.controls[controlName].touched) {
      return true;
    }
    return false;
  }

}