
import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { RiskLimitService } from '../../services/risk-limit.service';
import { DerogationService } from '../../services/derogation.service';
import { Counterparty, LimitType } from '../../models/risk-limit.model';
import { maxDerogationAmountValidator } from '../../validators/max-derogation-amount.validator';

interface DerogationFormControls {
  counterpartyId: number | null;
  limitType: LimitType | null;
  amount: number | null;
  reason: string | null;
  requestedBy: string | null;
}

@Component({
    selector: 'app-derogation-form',
    imports: [ReactiveFormsModule],
    templateUrl: './derogation-form.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './derogation-form.component.css'
})
export class DerogationFormComponent implements OnInit {

  counterparties: Counterparty[] = [];
  limitTypes: LimitType[] = ['CREDIT', 'MARKET', 'LIQUIDITY'];

  /** true tant qu'on n'a pas confirme qu'une limite existe pour le couple selectionne. */
  limitMissing = false;
  checkingLimit = false;

  submitted = false;
  submitError: string | null = null;
  submitSuccess = false;

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private riskLimitService: RiskLimitService,
    private derogationService: DerogationService
  ) {
    this.form = this.fb.group({
      counterpartyId: this.fb.control<number | null>(null, Validators.required),
      limitType: this.fb.control<LimitType | null>(null, Validators.required),
      amount: this.fb.control<number | null>(null, {
        validators: [Validators.required, Validators.min(0.01)],
        asyncValidators: [
          maxDerogationAmountValidator(
            this.derogationService,
            () => this.form.get('counterpartyId')?.value ?? null,
            () => this.form.get('limitType')?.value ?? null
          )
        ],
        updateOn: 'blur'
      }),
      reason: this.fb.control<string | null>(null, [Validators.required, Validators.minLength(20)]),
      requestedBy: this.fb.control<string | null>(null, [Validators.required, Validators.minLength(6)])
    });
  }

  ngOnInit(): void {
    this.riskLimitService.getCounterparties().subscribe((data) => (this.counterparties = data));

    this.form.get('counterpartyId')!.valueChanges.subscribe(() => this.onSelectionChange());
    this.form.get('limitType')!.valueChanges.subscribe(() => this.onSelectionChange());
  }

  private onSelectionChange(): void {
    const counterpartyId = this.form.get('counterpartyId')!.value;
    const limitType = this.form.get('limitType')!.value;

    this.form.get('amount')!.updateValueAndValidity();

    if (!counterpartyId || !limitType) {
      this.limitMissing = false;
      return;
    }

    this.checkingLimit = true;
    this.derogationService.checkLimit(counterpartyId, limitType).subscribe({
      next: (check) => {
        this.limitMissing = !check.limitExists;
        this.checkingLimit = false;
      },
      error: () => {
        this.checkingLimit = false;
      }
    });
  }

  get canSubmit(): boolean {
    return this.form.valid && !this.limitMissing && !this.checkingLimit;
  }

  submit(): void {
    this.submitted = true;
    this.submitError = null;
    this.submitSuccess = false;

    if (!this.canSubmit) {
      return;
    }

    const value = this.form.getRawValue() as DerogationFormControls;

    this.derogationService.create({
      counterpartyId: value.counterpartyId!,
      limitType: value.limitType!,
      amount: value.amount!,
      reason: value.reason!,
      requestedBy: value.requestedBy!
    }).subscribe({
      next: () => {
        this.submitSuccess = true;
        this.form.reset();
        this.submitted = false;
      },
      error: (err) => {
        this.submitError = err?.error?.message ?? "La demande de derogation n'a pas pu etre soumise.";
      }
    });
  }
}
