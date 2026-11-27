import { Button } from './Button'
import { Modal } from './Modal'

export function ConfirmDialog({
  open,
  onClose,
  onConfirm,
  title,
  message,
  confirmLabel = 'Confirm',
  loading = false,
}: {
  open: boolean
  onClose: () => void
  onConfirm: () => void
  title: string
  message: string
  confirmLabel?: string
  loading?: boolean
}) {
  return (
    <Modal open={open} onClose={onClose} title={title}>
      <p className="text-slate-600">{message}</p>
      <div className="mt-6 flex justify-end gap-3">
        <Button variant="secondary" disabled={loading} onClick={onClose}>Cancel</Button>
        <Button variant="danger" loading={loading} onClick={onConfirm}>{confirmLabel}</Button>
      </div>
    </Modal>
  )
}
