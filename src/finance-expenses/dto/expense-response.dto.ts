export class ExpenseResponse {
  id: string;
  userId: string;
  date: Date;
  amount: number;
  description: string;
  category?: string;
  paymentMethod?: string;
  vendor?: string;
  status?: string;
  createdAt: string;
  updatedAt: string;
  attachmentUrls: string[];
}
