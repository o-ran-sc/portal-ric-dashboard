/*-
 * ========================LICENSE_START=================================
 * O-RAN-SC
 * %%
 * Copyright (C) 2019 Nordix Foundation
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
import { Component, OnInit, ViewChild, Inject, AfterViewInit } from '@angular/core';
import { MatMenuTrigger } from '@angular/material/menu';
import { trigger, state, style, animate, transition } from '@angular/animations';

import { JsonPointer } from 'angular6-json-schema-form';
import { PolicyService } from '../services/policy/policy.service';
import { ErrorDialogService } from '../services/ui/error-dialog.service';
import { NotificationService } from './../services/ui/notification.service';

import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
    // tslint:disable-next-line:component-selector
    selector: 'rd-policy-instance-dialog',
    templateUrl: './policy-instance-dialog.component.html',
    styleUrls: ['./policy-instance-dialog.component.scss'],
    animations: [
        trigger('expandSection', [
            state('in', style({ height: '*' })),
            transition(':enter', [
                style({ height: 0 }), animate(100),
            ]),
            transition(':leave', [
                style({ height: '*' }),
                animate(100, style({ height: 0 })),
            ]),
        ]),
    ],
})
export class PolicyInstanceDialogComponent implements OnInit, AfterViewInit {

    formActive = false;
    isVisible = {
        form: true,
        output: false
    };

    jsonFormStatusMessage = 'Loading form...';
    jsonSchemaObject: any = {};
    jsonObject: any = {};


    jsonFormOptions: any = {
        addSubmit: true, // Add a submit button if layout does not have one
        debug: false, // Don't show inline debugging information
        loadExternalAssets: true, // Load external css and JavaScript for frameworks
        returnEmptyFields: false, // Don't return values for empty input fields
        setSchemaDefaults: true, // Always use schema defaults for empty fields
        defautWidgetOptions: { feedback: true }, // Show inline feedback icons
    };

    liveFormData: any = {};
    formValidationErrors: any;
    formIsValid = null;


    @ViewChild(MatMenuTrigger, { static: true }) menuTrigger: MatMenuTrigger;

    public policyInstanceId: string;
    public policyTypeName: string;
    private policyTypeId: number;

    constructor(
        private dataService: PolicyService,
        private errorService: ErrorDialogService,
        private notificationService: NotificationService,
        @Inject(MAT_DIALOG_DATA) private data: any) {
        this.formActive = false;
        this.policyInstanceId = this.data.instanceId;
        this.policyTypeName = this.data.name;
        this.policyTypeId = this.data.policyTypeId;
        this.parseJson();
    }

    ngOnInit() {
        this.jsonFormStatusMessage = 'Init';
        this.formActive = true;
    }

    ngAfterViewInit() {
    }

    onSubmit() {
        if (this.policyInstanceId == null) {
            this.policyInstanceId = Date.now().toString();
        }
        const policyJson: string = this.prettyLiveFormData;
        const self: PolicyInstanceDialogComponent = this;
        this.dataService.putPolicy(this.policyTypeId, this.policyInstanceId, policyJson).subscribe(
            {
                next() {
                    self.notificationService.success('Policy ' + self.policyTypeName + ':' + self.policyInstanceId + ' submitted');
                },
                error(error) {
                    self.errorService.displayError('updatePolicy failed: ' + error.message);
                },
                complete() { }
            });
    }

    public onChanges(data: any) {
        this.liveFormData = data;
    }

    get prettyLiveFormData() {
        return JSON.stringify(this.liveFormData, null, 2);
    }

    get schemaAsString() {
        return JSON.stringify(this.jsonSchemaObject, null, 2);
    }

    get jsonAsString() {
        return JSON.stringify(this.jsonObject, null, 2);
    }

    isValid(isValid: boolean): void {
        this.formIsValid = isValid;
    }

    validationErrors(data: any): void {
        this.formValidationErrors = data;
    }

    get prettyValidationErrors() {
        if (!this.formValidationErrors) { return null; }
        const errorArray = [];
        for (const error of this.formValidationErrors) {
            const message = error.message;
            const dataPathArray = JsonPointer.parse(error.dataPath);
            if (dataPathArray.length) {
                let field = dataPathArray[0];
                for (let i = 1; i < dataPathArray.length; i++) {
                    const key = dataPathArray[i];
                    field += /^\d+$/.test(key) ? `[${key}]` : `.${key}`;
                }
                errorArray.push(`${field}: ${message}`);
            } else {
                errorArray.push(message);
            }
        }
        return errorArray.join('<br>');
    }

    private parseJson() {
        try {
            this.jsonSchemaObject = JSON.parse(this.data.createSchema);
            if (this.data.instanceJson != null) {
                this.jsonObject = JSON.parse(this.data.instanceJson);
            }
        } catch (jsonError) {
            this.jsonFormStatusMessage =
                'Invalid JSON\n' +
                'parser returned:\n\n' + jsonError;
            return;
        }
    }

    public toggleVisible(item: string) {
        this.isVisible[item] = !this.isVisible[item];
    }

}


