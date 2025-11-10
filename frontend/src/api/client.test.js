import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, apiRequest, downloadCsv } from './client'

const ORIGINAL_FETCH = global.fetch
const ORIGINAL_URL = global.URL
const ORIGINAL_CREATE_ELEMENT = document.createElement

afterEach(() => {
  vi.restoreAllMocks()
  global.fetch = ORIGINAL_FETCH
  global.URL = ORIGINAL_URL
  document.createElement = ORIGINAL_CREATE_ELEMENT
})

describe('api/client', () => {
  it('performs json request and attaches authorization header', async () => {
    const responseBody = { success: true }
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: vi.fn().mockResolvedValue(responseBody),
    })

    const result = await apiRequest('/status', {}, 'token-123')

    expect(global.fetch).toHaveBeenCalledWith('/api/status', {
      headers: { Authorization: 'Bearer token-123' },
      method: 'GET',
    })
    expect(result).toEqual(responseBody)
  })

  it('throws ApiError when response is not ok', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
      json: vi.fn().mockResolvedValue({ message: 'boom' }),
    })

    await expect(apiRequest('/broken')).rejects.toEqual(
      new ApiError('boom', 500, { message: 'boom' })
    )
  })

  it('downloads CSV and triggers link click', async () => {
    const blobMock = new Blob(['data'], { type: 'text/csv' })
    const appendMock = vi.spyOn(document.body, 'appendChild')

    const anchor = ORIGINAL_CREATE_ELEMENT.call(document, 'a')
    const clickMock = vi.spyOn(anchor, 'click').mockImplementation(() => {})
    const removeMock = vi.spyOn(anchor, 'remove').mockImplementation(() => {})
    document.createElement = vi.fn().mockReturnValue(anchor)

    global.URL = {
      createObjectURL: vi.fn().mockReturnValue('blob://url'),
      revokeObjectURL: vi.fn(),
    }

    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      blob: vi.fn().mockResolvedValue(blobMock),
    })

    await downloadCsv('/export', 'token-abc')

    expect(global.fetch).toHaveBeenCalledWith('/api/export', {
      headers: { Accept: 'text/csv', Authorization: 'Bearer token-abc' },
    })
    expect(appendMock).toHaveBeenCalled()
    expect(clickMock).toHaveBeenCalled()
    expect(removeMock).toHaveBeenCalled()
    expect(global.URL.revokeObjectURL).toHaveBeenCalledWith('blob://url')
  })
})
