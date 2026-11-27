interface SpinnerProps {
  label?: string
  size?: 'sm' | 'md' | 'lg'
}

export function Spinner({ label = 'Loading', size = 'md' }: SpinnerProps) {
  const dimensions = { sm: 'h-4 w-4', md: 'h-6 w-6', lg: 'h-10 w-10' }
  return (
    <span
      aria-label={label}
      role="status"
      className={`inline-block animate-spin rounded-full border-2 border-current border-r-transparent ${dimensions[size]}`}
    />
  )
}
