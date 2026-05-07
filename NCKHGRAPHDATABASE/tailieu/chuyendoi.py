import fitz  # PyMuPDF (PDF)
import docx  # python-docx (DOCX)
import re
import os


# ==============================
# CONFIG
# ==============================
INPUT_FILE = r"/workspaces/DALNCNTT_Cyber_Crimes_Detections/NCKHGRAPHDATABASE/tailieu/baocaodaln.pdf"  # đổi sang .docx nếu cần
OUTPUT_MD = r"/workspaces/DALNCNTT_Cyber_Crimes_Detections/NCKHGRAPHDATABASE/tailieu/baocaoscam.md"


# ==============================
# FORMAT TEXT → MARKDOWN
# ==============================
def format_markdown(text):
    lines = text.split("\n")
    md_lines = []

    for line in lines:
        line = line.strip()

        if not line:
            md_lines.append("")
            continue

        # ===== CHƯƠNG =====
        if re.match(r"CHƯƠNG\s+\d+", line, re.IGNORECASE):
            md_lines.append(f"\n# {line.upper()}\n")
            continue

        # ===== MỤC LỚN (1.1, 2.3...) =====
        if re.match(r"\d+\.\d+\.", line):
            md_lines.append(f"\n## {line}\n")
            continue

        # ===== MỤC NHỎ =====
        if re.match(r"\d+\.\d+\.\d+", line):
            md_lines.append(f"\n### {line}\n")
            continue

        # ===== DANH MỤC =====
        if "DANH MỤC" in line.upper():
            md_lines.append(f"\n# {line}\n")
            continue

        # ===== BULLET =====
        if line.startswith("") or line.startswith("-"):
            md_lines.append(f"- {line[1:].strip()}")
            continue

        md_lines.append(line)

    return "\n".join(md_lines)


# ==============================
# PDF → TEXT
# ==============================
def pdf_to_text(pdf_path):
    doc = fitz.open(pdf_path)
    full_text = ""

    for i, page in enumerate(doc):
        text = page.get_text("text")
        full_text += f"\n\n---\n\n## 📄 Trang {i+1}\n\n{text}"

    return full_text


# ==============================
# DOCX → TEXT
# ==============================
def docx_to_text(docx_path):
    doc = docx.Document(docx_path)
    full_text = ""

    for i, para in enumerate(doc.paragraphs):
        text = para.text.strip()
        if text:
            full_text += text + "\n"

    return full_text


# ==============================
# MAIN CONVERT
# ==============================
def convert_to_markdown(input_path, output_path):
    ext = os.path.splitext(input_path)[1].lower()

    if ext == ".pdf":
        print("📄 Đang xử lý PDF...")
        text = pdf_to_text(input_path)

    elif ext == ".docx":
        print("📝 Đang xử lý DOCX...")
        text = docx_to_text(input_path)

    else:
        print("❌ Chỉ hỗ trợ PDF và DOCX")
        return

    md_text = format_markdown(text)

    with open(output_path, "w", encoding="utf-8") as f:
        f.write(md_text)

    print(f"✅ DONE → {output_path}")


# ==============================
# RUN
# ==============================
if __name__ == "__main__":
    convert_to_markdown(INPUT_FILE, OUTPUT_MD)