'use client';

import { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Input } from '@/components/ui/input';
import { fetchApi } from '@/lib/api/client';

interface AddFollowUpDialogProps {
  taskId: number;
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const contactMethodOptions = [
  { value: 'PHONE', label: '电话' },
  { value: 'SMS', label: '短信' },
  { value: 'EMAIL', label: '邮件' },
  { value: 'VISIT', label: '上门' },
  { value: 'OTHER', label: '其他' },
];

const contactResultOptions = [
  { value: 'NO_ANSWER', label: '未接通' },
  { value: 'PROMISED', label: '承诺还款' },
  { value: 'REFUSED', label: '拒绝还款' },
  { value: 'UNREACHABLE', label: '无法联系' },
  { value: 'WRONG_NUMBER', label: '号码错误' },
  { value: 'OTHER', label: '其他' },
];

export function AddFollowUpDialog({
  taskId,
  open,
  onClose,
  onSuccess,
}: AddFollowUpDialogProps) {
  const [formData, setFormData] = useState({
    contactMethod: 'PHONE',
    contactResult: 'NO_ANSWER',
    notes: '',
    nextAction: '',
    nextContactDate: '',
    promiseAmount: '',
    promiseDate: '',
  });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async () => {
    try {
      setSubmitting(true);
      
      await fetchApi(`/admin/collection/tasks/${taskId}/records`, {
        method: 'POST',
        body: JSON.stringify({
          contactMethod: formData.contactMethod,
          contactResult: formData.contactResult,
          notes: formData.notes,
          nextAction: formData.nextAction || null,
          nextContactDate: formData.nextContactDate || null,
          promiseAmount: formData.promiseAmount ? parseFloat(formData.promiseAmount) : null,
          promiseDate: formData.promiseDate || null,
          contactTime: new Date().toISOString(),
          operatorId: 1,
        }),
      });

      setFormData({
        contactMethod: 'PHONE',
        contactResult: 'NO_ANSWER',
        notes: '',
        nextAction: '',
        nextContactDate: '',
        promiseAmount: '',
        promiseDate: '',
      });
      
      onSuccess();
      onClose();
    } catch (error) {
      console.error('Failed to add record:', error);
      alert('添加跟进记录失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>添加跟进记录</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div>
            <Label htmlFor="contactMethod">联系方式</Label>
            <select
              id="contactMethod"
              className="w-full mt-1 p-2 border rounded"
              value={formData.contactMethod}
              onChange={(e) =>
                setFormData({ ...formData, contactMethod: e.target.value })
              }
            >
              {contactMethodOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <Label htmlFor="contactResult">联系结果</Label>
            <select
              id="contactResult"
              className="w-full mt-1 p-2 border rounded"
              value={formData.contactResult}
              onChange={(e) =>
                setFormData({ ...formData, contactResult: e.target.value })
              }
            >
              {contactResultOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <Label htmlFor="notes">备注</Label>
            <Textarea
              id="notes"
              className="mt-1"
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              placeholder="记录联系详情..."
              rows={3}
            />
          </div>

          <div>
            <Label htmlFor="nextAction">下一步行动</Label>
            <Input
              id="nextAction"
              className="mt-1"
              value={formData.nextAction}
              onChange={(e) =>
                setFormData({ ...formData, nextAction: e.target.value })
              }
              placeholder="例如: 3天后再次跟进"
            />
          </div>

          <div>
            <Label htmlFor="nextContactDate">下次联系日期</Label>
            <Input
              id="nextContactDate"
              type="date"
              className="mt-1"
              value={formData.nextContactDate}
              onChange={(e) =>
                setFormData({ ...formData, nextContactDate: e.target.value })
              }
            />
          </div>

          {formData.contactResult === 'PROMISED' && (
            <>
              <div>
                <Label htmlFor="promiseAmount">承诺还款金额</Label>
                <Input
                  id="promiseAmount"
                  type="number"
                  className="mt-1"
                  value={formData.promiseAmount}
                  onChange={(e) =>
                    setFormData({ ...formData, promiseAmount: e.target.value })
                  }
                  placeholder="¥0.00"
                />
              </div>

              <div>
                <Label htmlFor="promiseDate">承诺还款日期</Label>
                <Input
                  id="promiseDate"
                  type="date"
                  className="mt-1"
                  value={formData.promiseDate}
                  onChange={(e) =>
                    setFormData({ ...formData, promiseDate: e.target.value })
                  }
                />
              </div>
            </>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            取消
          </Button>
          <Button onClick={handleSubmit} disabled={submitting}>
            {submitting ? '提交中...' : '提交'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
