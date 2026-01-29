export class CompanyExpenseResponse {
  id: string;
  title: string;
  type: string;
  date: Date;
  amount?: number;
  description?: string;
  status?: string;
  createdAt: string;
  updatedAt: string;
  attachmentUrls: string[];
}
