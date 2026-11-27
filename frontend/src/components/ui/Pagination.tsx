import { Button } from './Button'

export function Pagination({
  page,
  totalPages,
  onPageChange,
  pageNumbersAreOneBased = false,
}: {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
  pageNumbersAreOneBased?: boolean
}) {
  if (totalPages <= 1) return null
  return (
    <nav aria-label="Pagination" className="flex items-center justify-center gap-4">
      <Button variant="secondary" disabled={page <= 0} onClick={() => onPageChange(page - 1)}>Previous</Button>
      <span className="text-sm text-slate-600" aria-live="polite">
        Page {pageNumbersAreOneBased ? page : page + 1} of {totalPages}
      </span>
      <Button variant="secondary" disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)}>Next</Button>
    </nav>
  )
}
