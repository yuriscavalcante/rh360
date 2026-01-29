export class SalaryResponse {
  id: string;
  userId: string;
  referenceMonth: string;
  grossAmount: number;
  netAmount?: number;
  discounts?: number;
  bonuses?: number;
  paidAt?: Date;
  notes?: string;
  status?: string;
  createdAt: string;
  updatedAt: string;
  attachmentUrls: string[];
}
