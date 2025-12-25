import string


def xxd(data: bytes, width: int = 16) -> str:
    lines = []
    for offset in range(0, len(data), width):
        chunk = data[offset:offset + width]

        # hex bytes grouped like xxd (2 bytes per group)
        hex_bytes = " ".join(
            chunk[i:i+2].hex()
            for i in range(0, len(chunk), 2)
        )

        # pad last line
        hex_bytes = hex_bytes.ljust(width * 2 + width // 2)

        # ASCII view
        ascii_part = "".join(
            chr(b) if chr(b) in string.printable and b >= 0x20 else "."
            for b in chunk
        )

        lines.append(f"{offset:08x}: {hex_bytes}  {ascii_part}")

    return "\n".join(lines)